package by.foranx;

import com.temenos.api.TStructure;
import com.temenos.api.exceptions.T24CoreException;
import com.temenos.t24.api.complex.eb.templatehook.TransactionContext;
import com.temenos.t24.api.hook.system.RecordLifecycle;
import com.temenos.t24.api.records.account.AccountRecord;
import com.temenos.t24.api.records.user.UserRecord;
import com.temenos.t24.api.system.DataAccess;
import com.temenos.t24.api.system.Session;
import com.temenos.t24.api.tables.ebapitmiscparam.EbApiTMiscParamRecord;
import com.temenos.t24.api.tables.ebapitmiscparam.NameClass;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import by.foranx.entity.Request;
import by.foranx.entity.Response;

public class FnxParamCopyFromAccount extends RecordLifecycle {
    
    DataAccess dataAccess = new DataAccess();
    
    @Override
    public void defaultFieldValues(String application, String currentRecordId, TStructure currentRecord,
            TStructure unauthorisedRecord, TStructure liveRecord, TransactionContext transactionContext) {

        UserRecord userRec = new UserRecord(dataAccess.getRecord("USER", new Session(this).getUserId()));
        AccountRecord accRec;
        try {
            accRec = new AccountRecord(dataAccess.getRecord("ACCOUNT", currentRecordId));            
        }
        catch (Exception e) {
            throw new T24CoreException("", "Account record with id like that does not exist!");
        }
        
        Session currentSession = new Session(this);
        
        int isLocal;
        if (currentSession.getLocalCurrency().equals(accRec.getCurrency().getValue())) 
                isLocal = 1;
        else
            isLocal = 0;
        
        String expireDateString = accRec.getLocalRefField("L.EXP.DATE").getValue();
        String issueDateString = accRec.getLocalRefField("L.ISS.DATE").getValue();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuuMMdd");
        LocalDate expireDate = LocalDate.parse(expireDateString, formatter);
        LocalDate issueDate = LocalDate.parse(issueDateString, formatter);

        Period period = Period.between(issueDate, expireDate);
        
        Request requestObject = new Request();
        requestObject.setCurrency(accRec.getCurrency().getValue());
        requestObject.setAcctNo(currentRecordId);
        requestObject.setCustNo(accRec.getCustomer().getValue());
        requestObject.setUsername(userRec.getUserName().getValue());
        requestObject.setIsLocal(isLocal);
        requestObject.setIssueDate(issueDateString);
        requestObject.setExpireDate(expireDateString);
        requestObject.setValDays(period.getDays());
        
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost request = new HttpPost("http://localhost:9089/testTask/check");
        StringEntity entity;
        
        String responseBody = new String();
        Response responseObject = new Response();
        
        try {
            entity = new StringEntity(requestObject.toJson(), ContentType.APPLICATION_JSON);
            request.setEntity(entity);
            
            CloseableHttpResponse response = httpClient.execute(request);
            HttpEntity responseEntity = response.getEntity();
            responseBody = EntityUtils.toString(responseEntity);
            responseObject = new Response().fromJson(responseBody);
        }
        catch (Exception e) {
            throw new T24CoreException("", "Error with getting response!");
        }
        
        EbApiTMiscParamRecord ebRecord = new EbApiTMiscParamRecord();
        Field[] responseFields = Response.class.getDeclaredFields();
        
        try {
            for (Field field : responseFields) {
                field.setAccessible(true);
                NameClass tempName = new NameClass();
                tempName.setName(field.getName());
                
                switch (field.getType().toString()) {
                    case "int":
                        tempName.addValue(field.get(responseObject).toString());
                        break;
                    case "class java.lang.String":
                        tempName.addValue((String) field.get(responseObject));
                        break;
                }
                field.setAccessible(false);
                
                ebRecord.addName(tempName);
            }
            currentRecord.set(ebRecord.toStructure());
        }
        catch (Exception e) {
            throw new T24CoreException("", "Can't parse from field array!");
        }
    }
}
