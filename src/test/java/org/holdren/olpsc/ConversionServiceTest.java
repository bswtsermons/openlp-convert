package org.holdren.olpsc;

import info.openlyrics.namespace._2009.song.Song;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConversionServiceTest
{
	@InjectMocks
	private ConversionService conversionService;
	
	@Mock
	private JAXBContext songJaxbContext;
	
	@Mock 
	private Marshaller marshaller;

	@Test
	public void givenSongWhenConvertToXmlStringThenReturnXmlString() throws JAXBException
	{
		when(songJaxbContext.createMarshaller()).thenReturn(marshaller);
		doAnswer(new Answer<Void>() {

			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable
			{
				Object[] args = invocation.getArguments();
				StringWriter sw = (StringWriter) args[1];
				sw.write("expected string");
				return null;
			}
		}).when(marshaller).marshal(any(), any(Writer.class));
		String converted = conversionService.convert(new Song());
		
		assertThat(converted).as("got back expected string").isEqualTo("expected string");
	}
	
	@Test
	public void givenMultilineSongInputWhenConvertToSongThenReturnSong() throws IOException, URISyntaxException, JAXBException
	{
		String title = "Amazing Grace";
		
		//String result = conversionService.convert(title, new String(Files.readAllBytes(Paths.get(ClassLoader.getSystemResource("test-input.txt").toURI()))));
		Song song = null;
		try (BufferedReader br = Files.newBufferedReader(Paths.get(ClassLoader.getSystemResource("test-input.txt").toURI())))
		{
			song = conversionService.convert(title, br);
		}
		
		assertThat(song).as("song not null").isNotNull();
		assertThat(song.getVersion()).as("song version set").isEqualTo("0.8");
		assertThat(song.getProperties()).as("properties not null");
		assertThat(song.getProperties().getTitles()).as("titles not null").isNotNull();
		assertThat(song.getProperties().getTitles().getTitle())
													.as("single title set").hasSize(1)
		                                            .as("title set").contains("Amazing Grace");
		
		assertThat(song.getLyrics()).as("lyrics not null").isNotNull();
		assertThat(song.getLyrics().getVerse()).as("verses not null").isNotNull()
		                                       .as("correct number of verses").hasSize(7)
		                                       .as("has all verse numbers").extracting("name").contains("v1", "v2", "v3", "v4", "v5", "v6", "v7");
		
		assertThat(song.getLyrics().getVerse()).as("verses not null").extracting("lines").isNotNull();
		assertThat(song.getLyrics().getVerse().get(0).getLines().getContent().get(0))
		  .as("first verse formatted properly").isEqualTo("Amazing grace! How sweet the sound<br/>That saved a wretch like me!<br/>I once was lost, but now am found;<br/>Was blind, but now I see.");
		
		// TODO more assertions on more lines
		
	}
	
	@Test
	public void givenXmlEncodedLineBreaksWhenConvertedThenMixedContentLineBreaks()
	{
		String xmlEncoded = "line1&lt;br/&gt;line2&lt;br/&gt;line3";
		String mixedContent = conversionService.convertLineBreaks(xmlEncoded);
		assertThat(mixedContent)
		  .as("lines converted from xml Encoding to mixed content").isEqualTo("line1<ns2:br/>line2<ns2:br/>line3");
	}
}
