package org.holdren.olpsc;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ConversionControllerTest
{
	@Autowired
	private MockMvc mockMvc;
	
	@Test
	public void whenVisitConvertThenShowConvert() throws Exception
	{
		MvcResult mvcResult = mockMvc.perform(get("/openlp/convert"))
		       .andExpect(status().isOk()).andReturn();
		
		assertNotNull("convert form model not null", mvcResult.getModelAndView().getModel().get("convertForm"));
		assertTrue("convert form placed into model", mvcResult.getModelAndView().getModel().get("convertForm").getClass().equals(ConvertForm.class));

		// TODO check view name returned?
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
		mockMvc.perform(post("/openlp/convert")
			       .contentType(MediaType.APPLICATION_FORM_URLENCODED)
			       .content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
			    		   new BasicNameValuePair("name", "Song Title"),
			    		   new BasicNameValuePair("input", "Input")
			       )))))
			   .andExpect(status().isOk());
		
	}

}
