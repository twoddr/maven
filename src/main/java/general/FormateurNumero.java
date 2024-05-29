package general;

public class FormateurNumero {
	private int	taille			= 4;
	private int	nombreAFormater	= 0;
	
	/**
	 * Constructeur à deux entrées les entrées sont enregistrées si la taille
	 * souhaitée est positive
	 * 
	 * @param nombre
	 *            c'est le nombre à formater
	 * @param tailleSouhaitee
	 *            c'est la taille désirée du string de sortie
	 * 
	 */
	public FormateurNumero(int nombre, int tailleSouhaitee) {
		if (tailleSouhaitee > 0) {
			taille = tailleSouhaitee;
			nombreAFormater = nombre;
		}
	}
	
	public String getNombreFormate() {
		
		String format = "%0" + taille + "d";
		return String.format(format, nombreAFormater);
		
	}
	
	public int getTaille() {
		return taille;
	}
	
	public int getNombreAFormater() {
		return nombreAFormater;
	}
	
	public void setNombreAFormater(int nombreAFormater) {
		this.nombreAFormater = nombreAFormater;
	}
	
	@Override
	public String toString() {
		return getNombreFormate();
	}
	
	/*
	 * méthodes get/set
	 */
	
}
