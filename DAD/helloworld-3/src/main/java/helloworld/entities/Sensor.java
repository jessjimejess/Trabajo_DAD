package helloworld.entities;

public class Sensor {
	private int idsensor;
	private int idusuario;
	private String value;
	
	public Sensor(int idsensor, int idusuario, String value) {
		super();
		this.idsensor = idsensor;
		this.idusuario = idusuario;
		this.value = value;
	}
	
	public Sensor() {
		this(0,0,"");
	}

	public int getIdsensor() {
		return idsensor;
	}

	public void setIdsensor(int idsensor) {
		this.idsensor = idsensor;
	}

	public int getIdusuario() {
		return idusuario;
	}

	public void setIdusuario(int idusuario) {
		this.idusuario = idusuario;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	
}
