// Discord4J - Unofficial wrapper for Discord API
// Copyright (c) 2015
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

package sx.blah.discord.util;

/**
 * @author x
 * @since 10/2/2015
 */

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import sx.blah.discord.Discord4J;

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

	public String makeRequest(String url, BasicNameValuePair... headers) {
		try {
			HttpUriRequest request = this.requestClass.getConstructor(String.class).newInstance(url);
			for (BasicNameValuePair header : headers) {
				request.addHeader(header.getName(), header.getValue());
			}
			return EntityUtils.toString(CLIENT.execute(request).getEntity());
		} catch (Exception e) {
			Discord4J.logger.error("Unable to make request to {}. ({})", url, e.getMessage());
			return null;
		}
	}

	public String makeRequest(String url, HttpEntity entity, BasicNameValuePair... headers) {
		try {
			if (HttpEntityEnclosingRequestBase.class.isAssignableFrom(this.requestClass)) {
				HttpEntityEnclosingRequestBase request = (HttpEntityEnclosingRequestBase)
						this.requestClass.getConstructor(String.class).newInstance(url);
				for (BasicNameValuePair header : headers) {
					request.addHeader(header.getName(), header.getValue());
				}
				request.setEntity(entity);
				return EntityUtils.toString(CLIENT.execute(request).getEntity());
			} else {
				Discord4J.logger.error("Tried to attach HTTP entity to invalid type! ({})",
						this.requestClass.getSimpleName());
			}
		} catch (Exception e) {
			Discord4J.logger.error("Unable to make request to {}. ({})", url, e.getMessage());
		}
		return null;
	}
}
