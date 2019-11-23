package com.bas.auction.core.auto.integra;

import com.google.gson.JsonObject;
import retrofit.Call;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

import java.util.Map;

public interface RestService {
	@FormUrlEncoded
	@POST("organization/create")
	Call<JsonObject> createOrganization(@FieldMap Map<String, Object> map);

	@FormUrlEncoded
	@POST("negotiation/create")
	Call<JsonObject> createNeg(@FieldMap  Map<String, Object> map);
}
