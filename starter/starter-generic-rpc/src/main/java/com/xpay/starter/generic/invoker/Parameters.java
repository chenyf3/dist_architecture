package com.xpay.starter.generic.invoker;

import java.util.ArrayList;
import java.util.List;

public class Parameters {
    private List<Parameter> parameterList = new ArrayList<>();

    public static Parameters newInstance(){
        return new Parameters();
    }

    public Parameters addParameter(String type, Object value){
        parameterList.add(new Parameter(type, value));
        return this;
    }

    public List<Parameter> getParameterList() {
        return parameterList;
    }

    public class Parameter {
        private String type;//参数类型，全限定类名，如：java.lang.String
        private Object value;

        public Parameter(String type, Object value){
            this.type = type;
            this.value = value;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }
}
