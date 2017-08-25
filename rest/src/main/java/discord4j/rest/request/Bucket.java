package discord4j.rest.request;

import discord4j.rest.util.RouteUtils;

import javax.annotation.Nullable;
import java.util.Objects;

public final class Bucket {

	private final String uriTemplate;
	@Nullable
	private final String majorParam;

	private Bucket(String uriTemplate, String completeUri) {
		this.uriTemplate = uriTemplate;
		this.majorParam = RouteUtils.getMajorParam(uriTemplate, completeUri);
	}

	public static Bucket of(String uriTemplate, String completeUri) {
		return new Bucket(uriTemplate, completeUri);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		Bucket bucket = (Bucket) obj;

		return uriTemplate.equals(bucket.uriTemplate) && Objects.equals(majorParam, bucket.majorParam);
	}

	@Override
	public int hashCode() {
		return Objects.hash(uriTemplate, majorParam);
	}
}
