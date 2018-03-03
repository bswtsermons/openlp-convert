package org.holdren.olpsc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.bswt.openlyricj.OpenLPImportWriter;
import org.bswt.openlyricj.model.Lyrics;
import org.bswt.openlyricj.model.Properties;
import org.bswt.openlyricj.model.Song;
import org.bswt.openlyricj.model.Verse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

public class OpenLPConverterController
{
	private static final Logger LOG = LoggerFactory.getLogger(OpenLPConverterController.class);
	
	private JAXBContext songJaxbContext;
	
	@Value("${openlyricconverter.emailRecipient:tbholdren@gmail.com}")
	private String emailRecipient;
	
	@Autowired
	private JavaMailSender mailSender;
	
	@PostConstruct
	private void init() throws JAXBException
	{
		songJaxbContext = JAXBContext.newInstance(Song.class);
	}
	
	@GetMapping("/convert")
	public String getConvert(Model model)
	{
		model.addAttribute("convertForm", new ConvertForm());
		return "convert";
	}
	
	@PostMapping("/convert")
	public HttpEntity<byte[]> downloadConvertedFile(@ModelAttribute ConvertForm convertForm, HttpServletResponse response) throws IOException, JAXBException
	{
		String openLyricXml = convertToOpenLyric(convertForm.getName(), convertForm.getInput());
		String name = String.format("\"%s.xml\"",  (StringUtils.isNotBlank(convertForm.getName())) ? convertForm.getName() : "sermon notes");
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_XML);
		headers.set(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=%s", name)); 
		//headers.setContentLength(openLyricXml.length()); // TODO fix this
		
		/*
		if (convertForm.isSendEmail() && false) // TODO debug obviously)
		{
			LOG.info("emailing copy of lyric xml to {}", emailRecipient);
			sendEmail(name, openLyricXml);
		}
		*/
		
		return new HttpEntity<byte[]>(openLyricXml.getBytes(), headers);
	}
	
	private void sendEmail(String name, String openLyricXml)
	{
		LOG.info("mail sender class: {}", mailSender.getClass());
		MimeMessage mimeMessage = mailSender.createMimeMessage();
		try
		{
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
			helper.setTo(emailRecipient);
	        helper.setText("Please import the attached notes into OpenLP for today's sermon.");
	        ByteArrayResource bar = new ByteArrayResource(openLyricXml.getBytes());
	        LOG.info("content length: {}", bar.contentLength());
	        helper.addAttachment(name, new ByteArrayResource(openLyricXml.getBytes()), MediaType.APPLICATION_XML_VALUE);
	        mailSender.send(mimeMessage);
		} 
		catch (MessagingException e)
		{
			LOG.error("could not send lyrics xml to {}", emailRecipient, e);
		}
		
	}
	
	private String convertToOpenLyric(String title, String text) throws JAXBException, IOException
	{
		List<Verse> verses = new ArrayList<Verse>();
		List<String> verseLines = new ArrayList<>();
		AtomicInteger verseNo = new AtomicInteger(0);
		
		BufferedReader br = new BufferedReader(new StringReader(text));
		String line = null;
		while ((line = br.readLine()) != null) {
			if (StringUtils.isBlank(line))
			{
				processVerseLines(verseLines, verseNo, verses);
			}
			else
			{
				verseLines.add(line);
			}
		}
	
		processVerseLines(verseLines, verseNo, verses);
		
		// verses gathered up.  good.
		Song song = new Song();
		Properties properties = new Properties();
		properties.setTitles(title);
		properties.setAuthors("Samuel Browning");
		song.setProperties(properties);
		
		Lyrics lyrics = new Lyrics();
		lyrics.setVerses(verses);
		song.setLyrics(lyrics);
		
		StringWriter sw = new StringWriter();
		StreamResult sr = new StreamResult(sw);
		
		Marshaller marshaller = songJaxbContext.createMarshaller();
		marshaller.marshal(song, sr);
		marshaller.marshal(song, System.out);
		
		return sw.toString().replaceAll("&lt;br/&gt;", "<br/>");
	}

	private void processVerseLines(List<String> verseLines, AtomicInteger verseNo, List<Verse> verses)
			throws JAXBException
	{
		// blank line? commit last group if needed
		if (0 < verseLines.size())
		{
			Verse verse = new Verse();
			verse.setName(String.format("v%s", verseNo.incrementAndGet()));
			verse.setLines(String.join("<br/>", verseLines));
			verses.add(verse);

			verseLines.clear();
		}
	
	
	}
}
