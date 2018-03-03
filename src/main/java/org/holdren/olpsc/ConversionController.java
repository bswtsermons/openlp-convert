package org.holdren.olpsc;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/openlp/convert")
public class ConversionController
{
	@ModelAttribute("convertForm")
	public ConvertForm getConvertForm()
	{
		return new ConvertForm();
	}
	
	@GetMapping
	public String displayConvert()
	{
		return "convert";
	}
	
	@PostMapping
	public Object convert(@Valid ConvertForm convertForm, BindingResult bindingResult)
	{
		if (bindingResult.hasErrors())
		{
			return "redirect:/openlp/convert";
		}
		else
		{
			return ResponseEntity.ok().build();
		}
	}
}
