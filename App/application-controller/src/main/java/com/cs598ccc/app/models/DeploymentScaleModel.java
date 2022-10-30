package com.cs598ccc.app.models;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

// generated using jsonschema2pojo.org

@Generated("jsonschema2pojo")
public class DeploymentScaleModel {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("action")
    @Expose
    private Integer action;

    /**
     * No args constructor for use in serialization
     *
     */
    public DeploymentScaleModel() {
    }

    /**
     *
     * @param action
     * @param name
     */
    public DeploymentScaleModel(String name, Integer action) {
        super();
        this.name = name;
        this.action = action;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAction() {
        return action;
    }

    public void setAction(Integer action) {
        this.action = action;
    }

}