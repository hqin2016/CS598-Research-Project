package com.cs598ccc.app.models;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class Metric {

    @SerializedName("kubernetes_name")
    @Expose
    private String kubernetesName;

    /**
     * No args constructor for use in serialization
     *
     */
    public Metric() {
    }

    /**
     *
     * @param kubernetesName
     */
    public Metric(String kubernetesName) {
        super();
        this.kubernetesName = kubernetesName;
    }

    public String getKubernetesName() {
        return kubernetesName;
    }

    public void setKubernetesName(String kubernetesName) {
        this.kubernetesName = kubernetesName;
    }

}