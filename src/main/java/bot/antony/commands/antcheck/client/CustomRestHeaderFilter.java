package bot.antony.commands.antcheck.client;

import java.io.IOException;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;

public class CustomRestHeaderFilter implements ClientRequestFilter {

	private final String name;
	private final String value;

	public CustomRestHeaderFilter(String name, String value) {
		this.name = name;
		this.value = value;
	}

	@Override
	public void filter(ClientRequestContext requestContext) throws IOException {
		requestContext.getHeaders().add(name, value);
	}
}