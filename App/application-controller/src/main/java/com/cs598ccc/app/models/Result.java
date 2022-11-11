package com.cs598ccc.app.models;

import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class Result {

    @SerializedName("metric")
    @Expose
    private Metric metric;
    @SerializedName("value")
    @Expose
    private List<Float> value = null;

    /**
     * No args constructor for use in serialization
     *
     */
    public Result() {
    }

    /**
     *
     * @param metric
     * @param value
     */
    public Result(Metric metric, List<Float> value) {
        super();
        this.metric = metric;
        this.value = value;
    }

    public Metric getMetric() {
        return metric;
    }

    public void setMetric(Metric metric) {
        this.metric = metric;
    }

    public List<Float> getValue() {
        return value;
    }

    public void setValue(List<Float> value) {
        this.value = value;
    }

}