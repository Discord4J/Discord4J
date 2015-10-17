/*
 * Discord4J - Unofficial wrapper for Discord API
 * Copyright (c) 2015
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package sx.blah.discord.util;

/**
 * @author x
 * @since 10/2/2015
 */

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import sx.blah.discord.Discord4J;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.NoSuchElementException;

/**
 * New Request system. Reflection is cool, right guys?
 * R-right...?
 */
public enum Requests {
    POST(HttpPost.class),
    GET(HttpGet.class),
    DELETE(HttpDelete.class),
    PATCH(HttpPatch.class);

    static final HttpClient CLIENT = HttpClients.createDefault();

    final Class<? extends HttpUriRequest> requestClass;

    Requests(Class<? extends HttpUriRequest> clazz) {
        this.requestClass = clazz;
    }

    public Class<? extends HttpUriRequest> getRequestClass() {
        return requestClass;
    }

    public String makeRequest(String url, BasicNameValuePair... headers) throws HTTP403Exception {
        try {
            HttpUriRequest request = this.requestClass.getConstructor(String.class).newInstance(url);
            for (BasicNameValuePair header : headers) {
                request.addHeader(header.getName(), header.getValue());
            }
            HttpResponse response = CLIENT.execute(request);
            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode == 404) {
                Discord4J.logger.error("Received 404 error, please notify the developer and include the URL ({})", url);
            } else if (responseCode == 403) {
                throw new HTTP403Exception("Unable to make request to " + url);
            }
            return EntityUtils.toString(response.getEntity());
        } catch (IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String makeRequest(String url, HttpEntity entity, BasicNameValuePair... headers) throws HTTP403Exception {
        try {
            if (HttpEntityEnclosingRequestBase.class.isAssignableFrom(this.requestClass)) {
                HttpEntityEnclosingRequestBase request = (HttpEntityEnclosingRequestBase)
                        this.requestClass.getConstructor(String.class).newInstance(url);
                for (BasicNameValuePair header : headers) {
                    request.addHeader(header.getName(), header.getValue());
                }
                request.setEntity(entity);
                HttpResponse response = CLIENT.execute(request);
                int responseCode = response.getStatusLine().getStatusCode();
                if (responseCode == 404) {
                    Discord4J.logger.error("Received 404 error, please notify the developer and include the URL ({})", url);
                } else if (responseCode == 403) {
                    throw new HTTP403Exception("Unable to make request to " + url);
                }
                return EntityUtils.toString(response.getEntity());
            } else {
                Discord4J.logger.error("Tried to attach HTTP entity to invalid type! ({})",
                        this.requestClass.getSimpleName());
            }
        } catch (IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }
}
