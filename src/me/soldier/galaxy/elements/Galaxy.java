package me.soldier.galaxy.elements;

import java.util.*;

import me.soldier.galaxy.core.*;
import static me.soldier.galaxy.elements.Constants.*;

//OK
public class Galaxy {

	static CumulativeDistributionFunction cdf = new CumulativeDistributionFunction();

	private ArrayList<Star> m_pStars;
	private ArrayList<Star> m_pDust;
	private ArrayList<Star> m_pH2;

	// Paramètres permetants de définir la structure générale de la galaxie
	/** Excentricté de l'éllipse interne */
	private double m_elEx1 = 0;
	/** Excentricté de l'éllipse externe */
	private double m_elEx2 = 0;

	/** Vitesse du centre du coeur en km/s */
	private double m_velOrigin = 0;
	/** Vitesse du bord du coeur en km/s */
	private double m_velInner = 0;
	/** Vitesse du bord du disque en km/s */
	private double m_velOuter = 0;

	/** Décalage angulaire en parsec */
	private double m_angleOffset = 0;

	/** Rayon du coeur */
	private double m_radCore = 0;
	/** Rayon de la galaxie */
	private double m_radGalaxy = 0;
	/**
	 * Rayon à partir duquel toutes les particules doivent avoir une forme
	 * ciculaire
	 */
	private double m_radFarField = 0;
	/** Répartition des étoiles */
	private double m_sigma = 0;

	/** Nombre d'étoiles */
	private int m_numStars = 0;
	/** Nombre de particules de poussière */
	private int m_numDust = 0;
	/** Nombre de région d'H2 */
	private int m_numH2 = 0;

	private double m_time = 0;
	private double m_timeStep = 0;

	/** Histogramme de la répartition des étoiles */
	public int[] m_numberByRad = new int[100];
	
	public ModelMatrix ml_matrix;

	public Galaxy(double rad, double radCore, double deltaAng, double ex1, double ex2, double velInner, double velOuter, int numStars) {
		m_elEx1 = ex1;
		m_elEx2 = ex2;
		m_velOrigin = 30;
		m_velInner = velInner;
		m_velOuter = velOuter;
		m_angleOffset = deltaAng;
		m_radCore = radCore;
		m_radGalaxy = rad;
		m_radFarField = m_radGalaxy * 2;
		m_sigma = 0.5;
		m_numStars = numStars;
		m_numDust = (int) (numStars / 2);
		m_numH2 = 200;
		m_time = 0;
		m_timeStep = 0;
		m_pStars = new ArrayList<Star>();
		m_pDust = new ArrayList<Star>();
		m_pH2 = new ArrayList<Star>();
		
		ml_matrix = new ModelMatrix();
		for (int i = 0; i < 100; i++) {
			this.m_numberByRad[i] = 0;
		}
		Reset();
	}

	public Galaxy() {
		this(16000, 4000, 0.0004, 0.9, 0.9, 200, 300, 50000);
	}

	public double rnd_spread(double v, double o) {
		return (v + (2 * o * Math.random() - o));
	}

	private void InitStars(double sigma) {
		m_pStars = new ArrayList<Star>();
		m_pDust = new ArrayList<Star>();
		m_pH2 = new ArrayList<Star>();
		for (int i = 0; i < m_numDust; i++) {
			m_pDust.add(new Star());
		}

		for (int i = 0; i < m_numStars; i++) {
			m_pStars.add(new Star());
		}

		for (int i = 0; i < m_numH2 * 2; i++) {
			m_pH2.add(new Star());
		}

		// Les trois premières étoiles permettent de calibrer la galaxie

		// La première étoile est le centre du trou noir au centre
		m_pStars.get(0).m_a = 0;
		m_pStars.get(0).m_b = 0;
		m_pStars.get(0).m_angle = 0;
		m_pStars.get(0).m_theta = 0;
		m_pStars.get(0).m_velTheta = 0;
		m_pStars.get(0).m_center = new Vector2f(0, 0);
		m_pStars.get(0).m_velTheta = GetOrbitalVelocity((m_pStars.get(0).m_a + m_pStars.get(0).m_b) / 2.0);
		m_pStars.get(0).m_temp = 6000;

		// La deuxième est au bord du coeur
		m_pStars.get(1).m_a = m_radCore;
		m_pStars.get(1).m_b = m_radCore * GetExcentricity(m_radCore);
		m_pStars.get(1).m_angle = GetAngularOffset(m_radCore);
		m_pStars.get(1).m_theta = 0;
		m_pStars.get(1).m_center = new Vector2f(0, 0);
		m_pStars.get(1).m_velTheta = GetOrbitalVelocity((m_pStars.get(1).m_a + m_pStars.get(1).m_b) / 2.0);
		m_pStars.get(1).m_temp = 6000;

		// La troisième est au bord du disque
		m_pStars.get(2).m_a = m_radGalaxy;
		m_pStars.get(2).m_b = m_radGalaxy * GetExcentricity(m_radGalaxy);
		m_pStars.get(2).m_angle = GetAngularOffset(m_radGalaxy);
		m_pStars.get(2).m_theta = 0;
		m_pStars.get(2).m_center = new Vector2f(0, 0);
		m_pStars.get(2).m_velTheta = GetOrbitalVelocity((m_pStars.get(2).m_a + m_pStars.get(2).m_b) / 2.0);
		m_pStars.get(2).m_temp = 6000;

		// Taille d'une cellule d'histogramme
		double dh = m_radFarField / 100.0;

		// Initialisation des étoiles
		cdf.SetupRealistic(1.0, 0.02, m_radGalaxy / 3.0, m_radCore, 0, m_radFarField, 1000);

		for (int i = 3; i < m_numStars; i++) {
			double rad = cdf.ValFromProp(Math.random());

			m_pStars.get(i).m_a = rad;
			m_pStars.get(i).m_b = rad * GetExcentricity(rad);
			m_pStars.get(i).m_angle = GetAngularOffset(rad);
			m_pStars.get(i).m_theta = 360.0 * Math.random();
			m_pStars.get(i).m_velTheta = GetOrbitalVelocity(rad);
			m_pStars.get(i).m_center = new Vector2f(0, 0);
			m_pStars.get(i).m_temp = 6000 + (6000 * Math.random()) - 3000;
			m_pStars.get(i).m_mag = 0.1 + 0.4 * Math.random();			
			
			if(i == 9) {
				System.out.println(m_radFarField);
			}
			
			int idx = (int) Math.floor(Math.min(1.0 / dh * (m_pStars.get(i).m_a + m_pStars.get(i).m_b) / 2.0, 99.0));
			m_numberByRad[idx]++;
		}

		// Initialisation de la poussière
		double x, y, rad;
		for (int i = 0; i < m_numDust; i++) {
			x = 2 * m_radGalaxy * Math.random() - m_radGalaxy;
			y = 2 * m_radGalaxy * Math.random() - m_radGalaxy;
			rad = Math.sqrt(x * x + y * y);

			m_pDust.get(i).m_a = rad;
			m_pDust.get(i).m_b = rad * GetExcentricity(rad);
			m_pDust.get(i).m_angle = GetAngularOffset(rad);
			m_pDust.get(i).m_theta = 360.0 * (Math.random());
			m_pDust.get(i).m_velTheta = GetOrbitalVelocity((m_pDust.get(i).m_a + m_pDust.get(i).m_b) / 2.0);
			m_pDust.get(i).m_center = new Vector2f(0, 0);
			m_pDust.get(i).m_temp = 6000 + rad / 4.0;

			if(Double.isInfinite(m_pDust.get(i).m_velTheta)) {
				System.out.println(i);
			}
			
			m_pDust.get(i).m_mag = 0.015 + 0.01 * Math.random();
			int idx = (int) Math.floor(Math.min(1.0 / dh * (m_pDust.get(i).m_a + m_pDust.get(i).m_b) / 2.0, 99.0));
			m_numberByRad[idx]++;
		}

		// Initialise les H2
		for (int i = 0; i < m_numH2; ++i) {
			x = 2 * m_radGalaxy * Math.random() - m_radGalaxy;
			y = 2 * m_radGalaxy * Math.random() - m_radGalaxy;
			rad = Math.sqrt(x * x + y * y);

			int k1 = 2 * i;
			m_pH2.get(k1).m_a = rad;
			m_pH2.get(k1).m_b = rad * GetExcentricity(rad);
			m_pH2.get(k1).m_angle = GetAngularOffset(rad);
			m_pH2.get(k1).m_theta = 360.0 * (Math.random());
			m_pH2.get(k1).m_velTheta = GetOrbitalVelocity((m_pH2.get(k1).m_a + m_pH2.get(k1).m_b) / 2.0);
			m_pH2.get(k1).m_center = new Vector2f(0, 0);
			m_pH2.get(k1).m_temp = 6000 + (6000 * (Math.random())) - 3000;
			m_pH2.get(k1).m_mag = 0.1 + 0.05 * Math.random();
			int idx = (int) Math.floor(Math.min(1.0 / dh * (m_pH2.get(k1).m_a + m_pH2.get(k1).m_b) / 2.0, 99.0)); // int
			m_numberByRad[idx]++;

			if(Double.isInfinite(m_pH2.get(k1).m_velTheta)) {
				System.out.println(i);
			}
			
			int k2 = 2 * i + 1;
			m_pH2.get(k2).m_a = rad + 1000;
			m_pH2.get(k2).m_b = rad * GetExcentricity(rad);
			m_pH2.get(k2).m_angle = m_pH2.get(k1).m_angle;
			m_pH2.get(k2).m_theta = m_pH2.get(k1).m_theta;
			m_pH2.get(k2).m_velTheta = m_pH2.get(k1).m_velTheta;
			m_pH2.get(k2).m_center = m_pH2.get(k1).m_center;
			m_pH2.get(k2).m_temp = m_pH2.get(k1).m_temp;
			m_pH2.get(k2).m_mag = m_pH2.get(k1).m_mag;
			idx = (int) Math.floor(Math.min(1.0 / dh * (m_pH2.get(k1).m_a + m_pH2.get(k1).m_b) / 2.0, 99.0));
			m_numberByRad[idx]++;
		}
	}

	public void Reset(double rad, double radCore, double deltaAng, double ex1, double ex2, double sigma, double velInner, double velOuter, int numStars) {
		m_elEx1 = ex1;
		m_elEx2 = ex2;
		m_velInner = velInner;
		m_velOuter = velOuter;
		m_angleOffset = deltaAng;
		m_radCore = radCore;
		m_radGalaxy = rad;
		m_radFarField = m_radGalaxy * 2;
		m_sigma = sigma;
		m_numStars = numStars;
		m_numDust = (int) (numStars / 2);
		m_time = 0;

		for (int i = 0; i < 100; i++) {
			this.m_numberByRad[i] = 0;
		}
		
		InitStars(m_sigma);
	}

	public void Reset() {
		Reset(m_radGalaxy, m_radCore, m_angleOffset, m_elEx1, m_elEx2, m_sigma, m_velInner, m_velOuter, m_numStars);
	}

	public ArrayList<Star> GetStars() {
		return m_pStars;
	}

	public ArrayList<Star> GetDust() {
		return m_pDust;
	}

	public ArrayList<Star> GetH2() {
		return m_pH2;
	}

	public double GetRad() {
		return m_radGalaxy;
	}

	public double GetCoreRad() {
		return m_radCore;
	}

	public double GetFarFieldRad() {
		return m_radFarField;
	}

	public double GetSigma() {
		return m_sigma;
	}

	public double GetOrbitalVelocity(double rad) {
		double vel_kms = 0;

		if (rad < m_radCore) {
			double dv = (m_velInner - m_velOrigin) / m_radCore;
			vel_kms = m_velOrigin + rad * dv;
		}
		else if (rad >= m_radCore) {
			double dv = (m_velOuter - m_velInner) / (m_radGalaxy - m_radCore);
			vel_kms = m_velInner + dv * (rad - m_radCore);
		}

		// Vitesse en degrés par ans
		double u = 2 * Math.PI * rad * PC_TO_KM;
		double time = u / (vel_kms * SEC_PER_YEAR);

		return 360.0 / time;
	}

	public double GetExcentricity(double rad) {
		if (rad < m_radCore) {
			return 1 + (rad / m_radCore) * (m_elEx1 - 1);
		}
		else if (rad > m_radCore && rad <= m_radGalaxy) {
			return m_elEx1 + (rad - m_radCore) / (m_radGalaxy - m_radCore) * (m_elEx2 - m_elEx1);
		}
		else if (rad > m_radGalaxy && rad <= m_radFarField) {
			return m_elEx2 + (rad - m_radGalaxy) / (m_radFarField - m_radGalaxy) * (1 - m_elEx2);
		} else {
			return 1;
		}
	}

	public double GetAngularOffset(double rad) {
		return rad * m_angleOffset;
	}

	public double GetAngularOffset() {
		return m_angleOffset;
	}

	public double GetExInner() {
		return m_elEx1;
	}

	public double GetExOuter() {
		return m_elEx2;
	}

	public double GetTimeStep() {
		return m_timeStep;
	}

	public double GetTime() {
		return m_time;
	}

	public int GetNumH2() {
		return m_numH2;
	}

	public int GetNumStars() {
		return m_numStars;
	}

	public int GetNumDust() {
		return m_numDust;
	}

	public void SingleTimeStep(double time) {
		m_timeStep = time;
		m_time += time;

		for (int i = 0; i < m_numStars; i++)
		{
			m_pStars.get(i).CalcXY(m_time);
		}

		for (int i = 0; i < m_numDust; i++)
		{
			m_pDust.get(i).CalcXY(m_time);
		}

		for (int i = 0; i < m_numH2 * 2; i++)
		{
			m_pH2.get(i).CalcXY(m_time);
		}
	}

	public Vector2f GetStarPos(int idx) {
		if (idx >= m_numStars)
			throw new IllegalArgumentException("Galaxy.GetStarPos: index out of bounds: " + idx);

		return m_pStars.get(idx).m_pos;
	}

	public void SetSigma(double s) {
		m_sigma = s;
		this.Reset();
	}

	public void SetAngularOffset(double offset) {
		m_angleOffset = offset;
		this.Reset();
	}

	public void SetCoreRad(double rad) {
		m_radCore = rad;
		this.Reset();
	}

	public void SetRad(double rad) {
		m_radGalaxy = rad;
		this.Reset();
	}

	public void SetExInner(double ex) {
		m_elEx1 = ex;
		this.Reset();
	}

	public void SetExOuter(double ex) {
		m_elEx2 = ex;
		this.Reset();
	}
}
