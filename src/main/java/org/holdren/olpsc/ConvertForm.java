package org.holdren.olpsc;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ConvertForm
{
	@NotNull
	private String name;
	
	@NotNull
	private String input;
	
	@NotNull
	private String minister;

	private boolean uploadToDropBox;

}
