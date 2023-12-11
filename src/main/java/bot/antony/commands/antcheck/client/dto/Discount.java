package bot.antony.commands.antcheck.client.dto;

public class Discount {
	private boolean available;
	
	private String code;
	
	private String condition;
	
	
	public boolean isAvailable() {
		return available;
	}
	
	public void setAvailable(boolean available) {
		this.available = available;
	}
	
	public String getCode() {
		return code;
	}
	
	public void setCode(String code) {
		this.code = code;
	}
	
	public String getCondition() {
		return condition;
	}
	
	public void setCondition(String condition) {
		this.condition = condition;
	}
	
	@Override
	public String toString() {
		return "Discount [available=" + available + ", code=" + code + ", condition=" + condition + "]";
	}
}
