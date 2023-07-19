package by.foranx.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Response {
    @JsonProperty("currency")
    private String currency;

    @JsonProperty("acctNo")
    private String acctNo;

    @JsonProperty("custNo")
    private String custNo;

    @JsonProperty("username")
    private String username;

    @JsonProperty("isLocal")
    private int isLocal;

    @JsonProperty("valDays")
    private int valDays;

    @JsonProperty("description")
    private String description;
    
    public Response fromJson(String json) throws JsonMappingException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, Response.class);
    }
    
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getAcctNo() {
        return acctNo;
    }

    public void setAcctNo(String acctNo) {
        this.acctNo = acctNo;
    }

    public String getCustNo() {
        return custNo;
    }

    public void setCustNo(String custNo) {
        this.custNo = custNo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getIsLocal() {
        return isLocal;
    }

    public void setIsLocal(int isLocal) {
        this.isLocal = isLocal;
    }

    public int getValDays() {
        return valDays;
    }

    public void setValDays(int valDays) {
        this.valDays = valDays;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
