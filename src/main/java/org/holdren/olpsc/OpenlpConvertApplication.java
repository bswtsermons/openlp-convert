package org.holdren.olpsc;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class OpenlpConvertApplication
{
	@Value("${dropBox.clientIdentifier:dropbox/0.01}")
	private String clientIdentifier;

	@Value("${dropBox.accessToken}")
	private String accessToken;

	public static void main(String[] args) 
	{
		SpringApplication.run(OpenlpConvertApplication.class, args);
	}

	@Bean
	public DbxClientV2 dbxClient()
	{
		return new DbxClientV2(DbxRequestConfig.newBuilder(clientIdentifier).build(), accessToken);
	}
}
