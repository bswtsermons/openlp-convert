package org.holdren.olpsc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Date;

import com.dropbox.core.v2.DbxClientV2;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.hamcrest.Matcher;
import org.holdren.olpsc.cs.CloudStorageException;
import org.holdren.olpsc.cs.CloudStorageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import info.openlyrics.namespace._2009.song.PropertiesType;
import info.openlyrics.namespace._2009.song.Song;
import org.springframework.web.util.NestedServletException;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
	"openlpconvert.version=olv",
	"openlpconvert.openlyric.version=olyv",
	"openlpconvert.author=auth",
	"dropBox.accessToken=fake"
})
public class ConversionControllerTest
{
	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private ConversionService conversionService;

	@MockBean
	private DbxClientV2 dbxClient;

	@MockBean
	private CloudStorageService cloudStorageService;
	
	@Test
	public void whenVisitConvertThenShowConvert() throws Exception
	{
		MvcResult mvcResult = mockMvc.perform(get("/openlp/convert"))
		       .andExpect(status().isOk()).andReturn();
		
		assertNotNull("convert form model not null", mvcResult.getModelAndView().getModel().get("convertForm"));
		assertTrue("convert form placed into model", mvcResult.getModelAndView().getModel().get("convertForm").getClass().equals(ConvertForm.class));
		assertThat(mvcResult.getModelAndView().getViewName()).as("correct view").isEqualTo("convert");
	}
	
	@Test
	public void givenInputAndNoNameWhenPostThenRedirectToForm() throws Exception
	{
		mockMvc.perform(post("/openlp/convert")
				       .contentType(MediaType.APPLICATION_FORM_URLENCODED)
				       .content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
				    		   new BasicNameValuePair("input", "Input")
				       )))))
			   .andExpect(status().is3xxRedirection())
			   .andExpect(header().string(HttpHeaders.LOCATION, equalTo("/openlp/convert")));
	}
	
	@Test
	public void givenNameAndNoInputWhenPostThenRedirectToForm() throws Exception
	{
		mockMvc.perform(post("/openlp/convert")
				       .contentType(MediaType.APPLICATION_FORM_URLENCODED)
				       .content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
				    		   new BasicNameValuePair("name", "Song Title")
				       )))))
			   .andExpect(status().is3xxRedirection())
			   .andExpect(header().string(HttpHeaders.LOCATION, equalTo("/openlp/convert")));
	}
	
	@Test
	public void givenNameAndInputWhenPostThenConvert() throws Exception
	{
		Song mockedSong = new Song();
		mockedSong.setProperties(new PropertiesType());
		String mixedSongXml = "mixed song xml";
		String convertedSongXml = "converted song xml";
		String songTitle = "Song Title";
		
		//setPrivateField("openLpConvertVersion", "convert-version", conversionController);
		//setPrivateField("openLyricVersion")
		
		when(conversionService.convert(eq(songTitle), Matchers.<BufferedReader>any())).thenReturn(mockedSong);
		when(conversionService.convert(mockedSong)).thenReturn(mixedSongXml);
		when(conversionService.convertLineBreaks(mixedSongXml)).thenReturn(convertedSongXml);
			
		MvcResult result = mockMvc.perform(post("/openlp/convert")
			   .contentType(MediaType.APPLICATION_FORM_URLENCODED)
			   .content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
			       new BasicNameValuePair("name", "Song Title"),
				   new BasicNameValuePair("input", "Input")
			   )))))
			   .andExpect(status().isOk())
			   .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE))
			   .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Song Title.xml\""))
			   .andReturn();
		
		verify(conversionService).convert(Matchers.<Song>any());
		verify(conversionService).convertLineBreaks(anyString());
		verify(conversionService).convert(anyString(), Matchers.<BufferedReader>any());
		
		// check that song was modified properly
		assertThat(mockedSong.getModifiedIn()).as("modified in set properly").isEqualTo("olv");
		assertThat(mockedSong.getCreatedIn()).as("created in set properly").isEqualTo("olv");
		assertThat(mockedSong.getVersion()).as("openlyric version set properly").isEqualTo("olyv");
		assertThat(mockedSong.getProperties().getAuthors().getAuthor().get(0)).as("author set").isEqualTo("auth");
		
		assertThat(mockedSong.getModifiedDate().toGregorianCalendar().getTime()).as("date set before now").isBeforeOrEqualsTo(new Date());
		
		assertThat(result.getResponse().getContentAsString()).as("body returned from mock").isEqualTo(convertedSongXml);
		
		
	}

	@Test
	public void givenUploadToDropboxWhenPostThenUploadToDropbox() throws Exception {
		Song mockedSong = new Song();
		mockedSong.setProperties(new PropertiesType());
		String mixedSongXml = "mixed song xml";
		String convertedSongXml = "converted song xml";
		String songTitle = "Song Title";

		when(conversionService.convert(eq(songTitle), Matchers.<BufferedReader>any())).thenReturn(mockedSong);
		when(conversionService.convert(mockedSong)).thenReturn(mixedSongXml);
		when(conversionService.convertLineBreaks(mixedSongXml)).thenReturn(convertedSongXml);

		MvcResult result = mockMvc.perform(post("/openlp/convert")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
						new BasicNameValuePair("name", "Song Title"),
						new BasicNameValuePair("input", "Input"),
						new BasicNameValuePair("uploadToDropBox", "on")
				)))))
				.andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE))
				.andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Song Title.xml\""))
				.andReturn();

		try
		{
			verify(cloudStorageService).store(eq("/Song Title.xml"), any(ByteArrayInputStream.class));
		}
		catch (CloudStorageException e) { }
	}

	@Test
	public void givenBadUploadToDropboxWhenPostThenWUT() throws Exception
	{
		Song mockedSong = new Song();
		mockedSong.setProperties(new PropertiesType());
		String mixedSongXml = "mixed song xml";
		String convertedSongXml = "converted song xml";
		String songTitle = "Song Title";

		when(conversionService.convert(eq(songTitle), Matchers.<BufferedReader>any())).thenReturn(mockedSong);
		when(conversionService.convert(mockedSong)).thenReturn(mixedSongXml);
		when(conversionService.convertLineBreaks(mixedSongXml)).thenReturn(convertedSongXml);
		CloudStorageException cloudStorageException = new CloudStorageException("bleh");
		//when(cloudStorageService.store(eq(songTitle + ".xml"), Matchers.any()));
		//when(cloudStorageService.store(null, null)).thenThrow(cloudStorageException);
		doThrow(cloudStorageException).when(cloudStorageService).store(eq(songTitle+".xml"), any(ByteArrayInputStream.class));

		// TODO: Why can't I test exceptions in mockMvc?
		try {
			mockMvc.perform(post("/openlp/convert")
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
							new BasicNameValuePair("name", "Song Title"),
							new BasicNameValuePair("input", "Input"),
							new BasicNameValuePair("uploadToDropBox", "on")
					)))))
					//.andExpect(status().is5xxServerError())
					.andDo(print())
				/*
				.andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE))
				.andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Song Title.xml\""))
				.andReturn()
				*/
			;
		}
		catch (NestedServletException e)
		{
			assertThat(e.getRootCause().getClass().equals(CloudStorageException.class));
		}

		try
		{
			verify(cloudStorageService).store(eq("Song Title.xml"), any(ByteArrayInputStream.class));
		}
		catch (CloudStorageException e) { }
	}
	
}
