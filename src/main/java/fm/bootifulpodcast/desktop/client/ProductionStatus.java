package fm.bootifulpodcast.desktop.client;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Log4j2
public class ProductionStatus {

	private Executor executor;

	private RestTemplate template;

	public ProductionStatus(Executor ex, RestTemplate rt, String errMsg,
			boolean published, String uid, HttpStatus status, URI statusUrl) {
		this.executor = ex;
		this.template = rt;
		this.uid = uid;
		Assert.notNull(this.executor, "the executor must be non-null");
		Assert.notNull(this.template,
				"the " + RestTemplate.class.getName() + " must be non-null");
		Assert.notNull(this.uid, "the UID must be non-null");
		this.errorMessage = errMsg;
		this.statusUrl = statusUrl;
		this.published = published;
		this.httpStatus = status;
	}

	public String getUid() {
		return uid;
	}

	public boolean isPublished() {
		return published;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public HttpStatus getHttpStatus() {
		return httpStatus;
	}

	public URI getStatusUrl() {
		return statusUrl;
	}

	private String uid;

	private boolean published;

	private String errorMessage;

	private HttpStatus httpStatus;

	private URI statusUrl;

	public CompletableFuture<URI> checkProductionStatus() {
		Assert.notNull(this.executor, "the executor must not be null");
		return CompletableFuture.supplyAsync(this::doPollProductionStatus, this.executor);
	}

	@SneakyThrows
	private URI doPollProductionStatus() {
		while (true) {
			var result = this.template.getForEntity(this.statusUrl, Map.class);
			Assert.isTrue(result.getStatusCode().is2xxSuccessful(),
					"the HTTP request must return a valid 20x series HTTP status");
			var status = (Map<String, String>) result.getBody();
			var key = "media-url";
			if (status.containsKey(key)) {
				return URI.create(status.get(key));
			}
			else {
				var seconds = 10;
				Thread.sleep(seconds * 1000);
				log.debug("sleeping " + seconds
						+ "s while checking the requestProductionStatus of '" + statusUrl
						+ "'.");
			}
		}
	}

}
