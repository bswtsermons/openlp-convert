package org.holdren.olpsc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.BufferedReader;
import java.util.Arrays;
import java.util.Date;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
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

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
	"openlpconvert.version=olv",
	"openlpconvert.openlyric.version=olyv",
	"openlpconvert.author=auth"
})
public class ConversionControllerTest
{
	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private ConversionService conversionService;
	
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
		
		//setPrivateField("openLpConvertVersion", "convert-version", conversionController);
		//setPrivateField("openLyricVersion")
		
		when(conversionService.convert(eq("Song Title"), Matchers.<BufferedReader>any())).thenReturn(mockedSong);
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
	
}
