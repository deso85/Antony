package bot.antony.commands.emergency;

import java.util.ArrayList;

public class EmergencyData {

	private String title;
	private String titleURI;
	private String description;
	private String imgURI;
	private ArrayList<ContentPair> content = new ArrayList<ContentPair>();
	
	// --------------------------------------------------
	// Constructor
	// --------------------------------------------------
	public EmergencyData() {
		super();
		
	}
	
	
	// --------------------------------------------------
	// Functions
	// --------------------------------------------------
	@Override
	public String toString() {
		//TODO: Code toString
		return "";
	}
	
	@Override
	public boolean equals(Object o) {
		// If the object is compared with itself then return true
		if (o == this) {
			return true;
		}
		// Check if o is an instance of UserData or not "null instanceof [type]" also returns false
		if(!(o instanceof EmergencyData)) {
			return false;
		}
		// Typecast o to UserData so that we can compare data
		EmergencyData emergencyData = (EmergencyData) o;
		// Compare the title
		if(getTitle() == emergencyData.getTitle() && getContent() == emergencyData.getContent()) {
			return true;
		}
		return false;
	}
	
	public boolean hasTitle() {
		if(title != null && title.length()>0) {
			return true;
		}
		return false;
	}
	
	public boolean hasTitleURI() {
		if(titleURI != null && titleURI.length()>0) {
			return true;
		}
		return false;
	}
	
	public boolean hasDescription() {
		if(description != null && description.length()>0) {
			return true;
		}		
		return false;
	}
	
	public boolean hasContent() {
		if(content.size()>0) {
			return true;
		}
		return false;
	}
	
	public boolean hasImage() {
		if(imgURI != null && imgURI.length()>0) {
			return true;
		}
		return false;
	}

	public void addContent(String title, String content) {
		this.content.add(new ContentPair(title, content));
	}
	
	public void addContent(String title, String content, boolean inline) {
		this.content.add(new ContentPair(title, content, inline));
	}
	
	// --------------------------------------------------
	// Getter & Setter
	// --------------------------------------------------
	public String getTitle() {
		return title;
	}


	public void setTitle(String title) {
		this.title = title;
	}


	public String getTitleURI() {
		return titleURI;
	}


	public void setTitleURI(String titleURI) {
		this.titleURI = titleURI;
	}

	
	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}


	public String getImgURI() {
		return imgURI;
	}


	public void setImgURI(String imgURI) {
		this.imgURI = imgURI;
	}


	public ArrayList<ContentPair> getContent() {
		return content;
	}


	public void setContent(ArrayList<ContentPair> content) {
		this.content = content;
	}
}


class ContentPair {

	private String title;
	private String content;
	private boolean inline;
	
	public ContentPair() {
		super();
	}
	
	public ContentPair(String title, String content) {
		this(title, content, false);
	}
	
	public ContentPair(String title, String content, boolean inline) {
		super();
		this.title = title;
		this.content = content;
		this.inline = inline;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public boolean isInline() {
		return inline;
	}

	public void setInline(boolean inline) {
		this.inline = inline;
	}
}