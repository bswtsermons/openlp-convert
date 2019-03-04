package org.holdren.olpsc;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.validation.Valid;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.holdren.olpsc.cs.CloudStorageException;
import org.holdren.olpsc.cs.CloudStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import info.openlyrics.namespace._2009.song.AuthorsType;
import info.openlyrics.namespace._2009.song.Song;

@Controller
@RequestMapping("/openlp/convert")
public class ConversionController
{
	@Value("${openlpconvert.version:OpenLP Convert v0.0.1}")
	private String openLpConvertVersion;
	
	@Value("${openlpconvert.openlyric.version:0.8}")
	private String openLyricVersion;
	
	@Value("${openlpconvert.author:Samuel Browning}")
	private String author;
	
	@Autowired
	private ConversionService conversionService;

	@Autowired
	private CloudStorageService cloudStorageService;
	
	@ModelAttribute("convertForm")
	public ConvertForm getConvertForm()
	{
		ConvertForm convertForm = new ConvertForm();
		convertForm.setMinister("Samuel Browning");
		return convertForm;
	}
	
	@GetMapping
	public String displayConvert()
	{
		return "convert";
	}
	
	@PostMapping
	public Object convert(@Valid ConvertForm convertForm, BindingResult bindingResult) throws IOException, JAXBException, DatatypeConfigurationException, CloudStorageException
	{
		if (bindingResult.hasErrors())
		{
			return "redirect:/openlp/convert";
		}
		else
		{
			Song song = conversionService.convert(convertForm.getName(), new BufferedReader(new StringReader(convertForm.getInput())));
			song.setCreatedIn(openLpConvertVersion);
			song.setModifiedIn(openLpConvertVersion);
			song.setModifiedDate(DatatypeFactory.newInstance().newXMLGregorianCalendar());
			song.setVersion(openLyricVersion);
			AuthorsType at = new AuthorsType();
			at.getAuthor().add(author);
			song.getProperties().setAuthors(at);
			GregorianCalendar gc = new GregorianCalendar();
			gc.setTime(new Date());
			song.setModifiedDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(gc));
			String songXmlString = conversionService.convertLineBreaks(conversionService.convert(song));

			if (convertForm.isUploadToDropBox())
			{
				try (ByteArrayInputStream is = new ByteArrayInputStream(songXmlString.getBytes(Charset.forName("UTF-8"))))
				{
					cloudStorageService.store(String.format("/%s.xml", convertForm.getName()), is);
				}
			}

			return ResponseEntity.ok()
								 .contentType(MediaType.APPLICATION_XML)
								 .header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s.xml\"", convertForm.getName()))
								 .body(songXmlString);
		}
	}
}
