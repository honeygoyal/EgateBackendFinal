package com.egatetutor.backend.enumType;


import java.io.Serializable;
import java.util.regex.Pattern;

public enum AttributeType implements Serializable
{
    SECTION,
    MARKS,
    NEGATIVE_MARKS,
    QUESTION_TYPE,
    QUESTION_LABEL,
    ANSWER,
    SOLUTION,
    DIFFICULTY,
    VIDEO_LINK;

    public String toString(){

        String typeS = "";
        switch (this)
        {
            case SECTION:
                typeS = "SECTION";
                break;
            case MARKS:
                typeS = "MARKS";
                break;
            case NEGATIVE_MARKS:
                typeS = "NEGATIVE MARKS";
                break;
            case QUESTION_TYPE:
                typeS = "TYPE";
                break;
            case QUESTION_LABEL:
                typeS = "QUESTION LABEL";
                break;
            case ANSWER:
                typeS = "ANSWER";
                break;
            case SOLUTION:
                typeS = "SOLUTION";
                break;
            case DIFFICULTY:
                typeS = "DIFFICULTY";
                break;
            case VIDEO_LINK:
                typeS = "VIDEO LINK";
                break;
        }

      return typeS;
    }

    public static AttributeType find(String value)
    {
        AttributeType type = null;
        if(value.equalsIgnoreCase(SECTION.toString())) type = AttributeType.SECTION;
        else if(Pattern.matches("\\d+", value)) type = AttributeType.QUESTION_LABEL;
        else if(value.equalsIgnoreCase(MARKS.toString())) type = AttributeType.MARKS;
        else if(value.equalsIgnoreCase(NEGATIVE_MARKS.toString())) type = AttributeType.NEGATIVE_MARKS;
        else if(value.equalsIgnoreCase(QUESTION_TYPE.toString())) type = AttributeType.QUESTION_TYPE;
        else if(value.equalsIgnoreCase(ANSWER.toString())) type = AttributeType.ANSWER;
        else if(value.equalsIgnoreCase(SOLUTION.toString())) type = AttributeType.SOLUTION;
        else if(value.equalsIgnoreCase(DIFFICULTY.toString())) type = AttributeType.DIFFICULTY;
        else if(value.equalsIgnoreCase(VIDEO_LINK.toString())) type = AttributeType.VIDEO_LINK;
        return type;
    }

}
