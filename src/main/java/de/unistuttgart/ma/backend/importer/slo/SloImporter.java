package de.unistuttgart.ma.backend.importer.slo;

import java.util.Set;
import de.unistuttgart.gropius.slo.SloRule;

/**
 * imports SLOs.
 * 
 * @author maumau
 *
 */
public interface SloImporter {
	public Set<SloRule> parse();
}
