/**
 * 
 */
package tml.conceptmap.ui;

import java.net.URL;

import javax.swing.JLabel;

import prefuse.util.ui.JPrefuseApplet;

/**
 * @author jorge
 *
 */
public class PrefuseApplet extends JPrefuseApplet {

	private static final long serialVersionUID = -2884252304502208433L;
	
	protected String externalid = null;
	
	@Override
	public void init() {
		super.init();
		
		try {
			System.out.println(getCodeBase().getHost());
			URL url = new URL(getCodeBase().getHost());
			String graphml = url.getContent().toString();
			System.out.println(graphml);
		} catch (Exception e1) {
			e1.printStackTrace();
			return;
		}
		externalid = getParameter("extid");
		PrefuseMap pmap;
		try {
			pmap = new PrefuseMap(externalid, this.getWidth(), this.getHeight());
			add(pmap.getDisplay());
		} catch (Exception e) {
			e.printStackTrace();
			add(new JLabel("Error loading PrefuseMap"));
		}
	}
}
