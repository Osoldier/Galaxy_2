package me.soldier.galaxy.elements;

import java.util.*;

//Corriger ValFromProp et PropFromVal
public class CumulativeDistributionFunction {

	double m_fMin = 0;
	double m_fMax = 0;
	double m_fWidth = 0;
	int m_nSteps = 0;

	/** Central Intensity */
	double m_I0 = 0;
	double m_k = 0;
	double m_a = 0;
	double m_RBulge = 0;

	ArrayList<Double> m_vM1;
	ArrayList<Double> m_vY1;
	ArrayList<Double> m_vX1;

	ArrayList<Double> m_vM2;
	ArrayList<Double> m_vY2;
	ArrayList<Double> m_vX2;

	public double PropFromVal(double fVal) {
		if (fVal < m_fMin || fVal > m_fMax)
			throw new IllegalArgumentException("fVal out of range");
		double h = 2 * ((m_fMax - m_fMin) / m_nSteps);
		int i = (int) Math.floor((fVal - m_fMin) / h);
		double remainder = fVal - i * h;
		
		return (m_vY1.get(i) + m_vM1.get(i) * remainder); 
	}
	
	public double ValFromProp(double fVal) {
		if(fVal < 0 || fVal > 1)
			throw new IllegalArgumentException("fVal out of range");
		
		double h = 1.0 / m_vY2.size();
		
		int i = (int) Math.floor(fVal / h);
		double remainder = fVal - i * h;
		//FIX
		if(i >= 999) i = 998;
		
		return (m_vY2.get(i) + m_vM2.get(i) * remainder);
	}
	
	public void SetupRealistic(double I0, double k, double a, double RBulge, double min, double max, int nSteps) {
		m_fMin = min;
		m_fMax = max;
		m_nSteps = nSteps;
		
		m_I0 = I0;
		m_k = k;
		m_a = a;
		m_RBulge = RBulge;
		
		BuildCDF(m_nSteps);
	}

	private void BuildCDF(int pnSteps) {
		double h = (m_fMax - m_fMin) / pnSteps;
		double x = 0, y = 0;

		m_vX1 = new ArrayList<Double>();
		m_vY1 = new ArrayList<Double>();
		m_vM1 = new ArrayList<Double>();

		m_vX2 = new ArrayList<Double>();
		m_vY2 = new ArrayList<Double>();
		m_vM2 = new ArrayList<Double>();

		// Simpson
		m_vY1.add(0.0);
		m_vX1.add(0.0);

		for (int i = 0; i < pnSteps; i += 2) {
			x = (i + 2) * h;
			y += h / 3 * (Intensity(m_fMin + i * h) + 4 * Intensity(m_fMin + (i + 1) * h) + Intensity(m_fMin + (i + 2) * h));

			m_vM1.add((y - m_vY1.get(m_vY1.size() - 1) / (2 * h)));
			m_vX1.add(x);
			m_vY1.add(y);
		}

		double m_vY1_last = m_vY1.get(m_vY1.size() - 1);
		for (int i = 0; i < m_vY1.size()-1; i++) {
			m_vY1.set(i, m_vY1.get(i) / m_vY1_last);
			m_vM1.set(i, m_vM1.get(i) / m_vY1_last);
		}

		m_vY2.add(0.0);

		double p = 0.0;
		h = 1.0 / pnSteps;
		for (int i = 1, k = 0; i < pnSteps; ++i) {
			p = i * h;

			// for(;m_vY1.get(k + 1) <= p; ++k) {
			//
			// }

			y = m_vX1.get(k) + (p - m_vY1.get(k)) / m_vM1.get(k);

			m_vM2.add((y - m_vY2.get(m_vY2.size() - 1)) / h);
			m_vX2.add(p);
			m_vY2.add(y);
		}
	}

	/**
	 * <a href="http://en.wikipedia.org/wiki/De_Vaucouleurs%27_law">Loi de
	 * Vaucouleurs </a>
	 */
	private double IntensityBulge(double R, double I0, double k) {
		return I0 * Math.exp(-k * Math.pow(R, 0.25));
	}

	/**
	 * <a href=
	 * "http://articles.beltoforion.de/article.php?a=spiral_galaxy_renderer&hl=en&s=idDist#idDist"
	 * >Freeman</a>
	 */
	private double IntensityDisc(double R, double I0, double a) {
		return I0 * Math.exp(-R / a);
	}

	private double Intensity(double x) {
		return (x < m_RBulge) ? IntensityBulge(x, m_I0, m_k) : IntensityDisc(x - m_RBulge, IntensityBulge(m_RBulge, m_I0, m_k), m_a);
	}
}
