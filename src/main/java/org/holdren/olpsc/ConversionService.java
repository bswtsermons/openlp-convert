package org.holdren.olpsc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import info.openlyrics.namespace._2009.song.LinesType;
import info.openlyrics.namespace._2009.song.LyricsType;
import info.openlyrics.namespace._2009.song.PropertiesType;
import info.openlyrics.namespace._2009.song.Song;
import info.openlyrics.namespace._2009.song.TitlesType;
import info.openlyrics.namespace._2009.song.VerseType;

@Service("OpenLpConversionService")
public class ConversionService
{
	private JAXBContext songJaxbContext;
	
	@PostConstruct
	public void init() throws JAXBException
	{
		songJaxbContext = JAXBContext.newInstance(Song.class);
	}
	
	public String convertLineBreaks(String xmlEncoded)
	{
		// TODO: this is a nasty cheat that needs fixed
		return xmlEncoded.replaceAll("&lt;br/&gt;", "<ns2:br/>");
	}
	
	public String convert(Song song) throws JAXBException
	{
		StringWriter sw = new StringWriter();
		
		Marshaller marshaller = songJaxbContext.createMarshaller();
		marshaller.marshal(song, sw);
		
		return sw.toString();
	}
	
	public Song convert(String title, BufferedReader br) throws IOException, JAXBException
	{
		TitlesType titles = new TitlesType();
		titles.getTitle().add(title);
		
		PropertiesType properties = new PropertiesType();
		properties.setTitles(titles);
		
		LyricsType lyrics = new LyricsType();
		lyrics.getVerse().addAll(parseVerses(br));
		
		Song song = new Song();
		song.setProperties(properties);
		song.setLyrics(lyrics);

		return song;
	}
	
	private List<VerseType> parseVerses(BufferedReader br) throws IOException, JAXBException
	{
		List<VerseType> verses = new ArrayList<VerseType>();
		List<String> verseLines = new ArrayList<>();
		AtomicInteger verseNo = new AtomicInteger(0);
		
		String line;
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
		
		return verses;
	}
	
	private void processVerseLines(List<String> verseLines, AtomicInteger verseNo, List<VerseType> verses)
	{
		// blank line? commit last group if needed
		if (!CollectionUtils.isEmpty(verseLines))
		{
			LinesType lines = new LinesType();
			lines.getContent().add(String.join("<br/>", verseLines));
			
			VerseType verse = new VerseType();
			verse.setName(String.format("v%s", verseNo.incrementAndGet()));
			verse.setLines(lines);
			verses.add(verse);

			verseLines.clear();
		}
	
	
	}
	
}
