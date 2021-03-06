package com.rtlabs.reqtool.ui.highlighter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.databinding.validation.ValidationStatus;

import com.google.common.collect.ImmutableList;
import com.rtlabs.reqtool.util.Result;

/**
 * Highlighter and validator for user stories.
 * 
 * The syntax rules are taken from https://en.wikipedia.org/wiki/User_story.
 */
public class UserStoryHighlighter {
	private static final Pattern AS_A    = Pattern.compile("\\bAs( a)?\\b", Pattern.CASE_INSENSITIVE);
	private static final Pattern I_WANT  = Pattern.compile("\\b(I want( to)?)|(I can)\\b", Pattern.CASE_INSENSITIVE);
	private static final Pattern SO_THAT = Pattern.compile("\\b((so that)|(because))( (I|(my)))?\\b", Pattern.CASE_INSENSITIVE);
	
	public static Result<String> highlight(String text) {
		StringBuilder output = new StringBuilder();
		Matcher m = AS_A.matcher(text);
		
		if (!m.find()) {
			return new Result<>(text, ImmutableList.of(ValidationStatus.error("A user story should start with the phrase \"As a <role>...\".")));
		}
		
		output.append(text, 0, m.start());
		output.append("<strong>").append(m.group()).append("</strong>");

		int asaEnd = m.end();
		m.usePattern(I_WANT);
		
		if (!m.find()) {
			return new Result<>(output.append(text, asaEnd, text.length()).toString(),
				ImmutableList.of(ValidationStatus.error("A user story should contain the phrase \"...I want...\" or \"...I can...\".")));
		}
		
		output.append(text, asaEnd, m.start());
		output.append("<strong>").append(m.group()).append("</strong>");

		int iwantEnd = m.end();
		m.usePattern(SO_THAT);

		if (!m.find()) {
			return new Result<>(output.append(text, iwantEnd, text.length()).toString());
		}
		
		output.append(text, iwantEnd, m.start());
		output.append("<strong>").append(m.group()).append("</strong>");

		output.append(text, m.end(), text.length());
		
		return new Result<>(output.toString());
	}
}
