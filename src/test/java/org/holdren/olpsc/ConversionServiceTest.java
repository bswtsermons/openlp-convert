package org.holdren.olpsc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class ConversionServiceTest
{
	private ConversionService conversionService;
	
	@Before
	public void setUp()
	{
		conversionService = new ConversionService();
	}
	
	@Test
	public void givenGoodInputsThenConvert()
	{
		String input = "foo";
		String result = conversionService.apply(input);
		
		assertNotNull("result wasn't null", result);
		//assertTrue("result not blank", StringUtils.isNotBlank(result));
		assertThat(StringUtils.isNotBlank(result)).as("result not blank").isTrue();
		
		Document doc = null;
		try
		{
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(result.getBytes()));
		}
		catch (SAXException | IOException | ParserConfigurationException e)
		{
			fail("could not parse resultant document as XML", e);
		}
	}

}
