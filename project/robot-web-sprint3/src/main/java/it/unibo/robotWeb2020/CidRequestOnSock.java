package it.unibo.robotWeb2020;

public class CidRequestOnSock {

	private String name;
	private String cid;

	public CidRequestOnSock() {
	}

	public CidRequestOnSock(String name, String cid) {
		this.name = name;
		this.cid = cid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}
}
