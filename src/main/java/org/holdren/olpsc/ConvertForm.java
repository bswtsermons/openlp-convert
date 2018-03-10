package org.holdren.olpsc;

import javax.validation.constraints.NotNull;

public class ConvertForm
{
	@NotNull
	private String name;
	
	@NotNull
	private String input;
	
	@NotNull
	private String minister;
	
	public String getMinister()
	{
		return minister;
	}

	public void setMinister(String minister)
	{
		this.minister = minister;
	}

	public String getInput()
	{
		return input;
	}

	public void setInput(String input)
	{
		this.input = input;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
	
	
}
