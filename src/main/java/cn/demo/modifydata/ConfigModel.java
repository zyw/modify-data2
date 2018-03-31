package cn.demo.modifydata;

import java.io.Serializable;

public class ConfigModel implements Serializable {

    private Integer rbfStart;
    private Integer rbfEnd;

    private Integer rzStart;
    private Integer rzEnd;

    private String licence;

    public Integer getRbfStart() {
        return rbfStart;
    }

    public void setRbfStart(Integer rbfStart) {
        this.rbfStart = rbfStart;
    }

    public Integer getRbfEnd() {
        return rbfEnd;
    }

    public void setRbfEnd(Integer rbfEnd) {
        this.rbfEnd = rbfEnd;
    }

    public Integer getRzStart() {
        return rzStart;
    }

    public void setRzStart(Integer rzStart) {
        this.rzStart = rzStart;
    }

    public Integer getRzEnd() {
        return rzEnd;
    }

    public void setRzEnd(Integer rzEnd) {
        this.rzEnd = rzEnd;
    }

    public String getLicence() {
        return licence;
    }

    public void setLicence(String licence) {
        this.licence = licence;
    }

    @Override
    public String toString() {
        return "ConfigModel{" +
                "rbfStart=" + rbfStart +
                ", rbfEnd=" + rbfEnd +
                ", rzStart=" + rzStart +
                ", rzEnd=" + rzEnd +
                '}';
    }
}
