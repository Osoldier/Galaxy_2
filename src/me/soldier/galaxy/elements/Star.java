package me.soldier.galaxy.elements;

import static me.soldier.galaxy.elements.Constants.*;
import me.soldier.galaxy.core.*;

//OK
public class Star {

	/** Position sur l'éllipse */
	public double m_theta = 0;
	/** Vecteur vitesse angulaire */
	public double m_velTheta = 0;
	/** Angle de l'éllipse */
	public double m_angle = 0;
	/** petit demi-axe */
	public double m_a = 0;
	/** Grand demi-axe */
	public double m_b;
	/** Température (couleur) */
	public double m_temp = 0;
	/** Luminosité */
	public double m_mag = 0;
	/** Centre de l'orbite */
	public Vector2f m_center = new Vector2f();
	/** Vitesse actuelle */
	public Vector2f m_vel = new Vector2f();
	/** Position actuelle */
	public Vector2f m_pos = new Vector2f();

	public Vector2f CalcXY(double time) {
		double a = m_a;
		double b = m_b;

		double theta = m_theta + (m_velTheta * time);
		Vector2f p = m_center;
		
		double beta = -m_angle;
		double alpha = theta * DEG_TO_RAD;

		double cosalpha = Math.cos(alpha);
		double sinalpha = Math.sin(alpha);
		double cosbeta = Math.cos(beta);
		double sinbeta = Math.sin(beta);

		Vector2f posOld = new Vector2f(m_pos.x, m_pos.y);
		m_pos.x = (float) (p.x + (a * cosalpha * cosbeta - b * sinalpha * sinbeta));
		m_pos.y = (float) (p.y + (a * cosalpha * sinbeta + b * sinalpha * cosbeta));
		
		m_vel.x = m_pos.x - posOld.x;
		m_vel.y = m_pos.y - posOld.y;
				
		return m_pos;
	}
	
	

}
