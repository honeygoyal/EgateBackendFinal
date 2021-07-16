package com.egatetutor.backend.enumType;

import java.util.regex.Pattern;

public enum QuestionType {
MSQ, NAT, MCQ, TF;

    public static QuestionType find(String value)
    {
        QuestionType type = null;
        if(value.equalsIgnoreCase(NAT.name())) type = QuestionType.NAT;
        else if(value.equalsIgnoreCase(MSQ.name())) type = QuestionType.MSQ;
        else if(value.equalsIgnoreCase(TF.name()))type = QuestionType.TF;
        else type = QuestionType.MCQ;
        return type;
    }
}
