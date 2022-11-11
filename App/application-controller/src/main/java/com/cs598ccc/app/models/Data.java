package com.cs598ccc.app.models;

import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class Data {

    @SerializedName("resultType")
    @Expose
    private String resultType;
    @SerializedName("result")
    @Expose
    private List<Result> result = null;

    /**
     * No args constructor for use in serialization
     *
     */
    public Data() {
    }

    /**
     *
     * @param result
     * @param resultType
     */
    public Data(String resultType, List<Result> result) {
        super();
        this.resultType = resultType;
        this.result = result;
    }

    public String getResultType() {
        return resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public List<Result> getResult() {
        return result;
    }

    public void setResult(List<Result> result) {
        this.result = result;
    }

}