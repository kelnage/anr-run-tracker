/*
 * Copyright (C) 2013 Nick Moore
 *
 * This file is part of ANR Run Tracker
 *
 * ANR Run Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.org.nickmoore.runtrack.controller;

import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import uk.org.nickmoore.runtrack.model.NetrunnerDBCard;

/**
 *
 */
public class IdentityFetcher extends AsyncTask<Void, Void, List<NetrunnerDBCard>> {
    public static String API_ENDPOINT = "http://netrunnerdb.com/api/cards/";

    @Override
    protected List<NetrunnerDBCard> doInBackground(Void... params) {
        HttpGet request = new HttpGet(API_ENDPOINT);
        HttpClient client = new DefaultHttpClient();
        try {
            HttpResponse response = client.execute(request);
            JSONArray array = new JSONArray(EntityUtils.toString(response.getEntity(), "ASCII"));
            for(int i = 0; i < array.length(); i++) {
                JSONObject card = array.getJSONObject(i);
                if(card.getString("type_code").equals("identity") &&
                        !card.getString("set_code").equals("alt") &&
                        !card.getString("set_code").equals("special")) {

                }
            }
        } catch(IOException ioex) {
            // TODO
        } catch(JSONException jsonex) {
            // TODO
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<NetrunnerDBCard> netrunnerDBCards) {
        super.onPostExecute(netrunnerDBCards);
    }
}
