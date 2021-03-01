package es.alba.sweet.client.scan;

import java.util.ArrayList;
import java.util.List;

import es.alba.sweet.base.configuration.AFileConfiguration;
import es.alba.sweet.base.scan.Header;
import es.alba.sweet.client.core.DirectoryLocator;
import es.alba.sweet.client.core.constant.Directory;
import es.alba.sweet.client.scan.graph.Legend;

public class ScanConfiguration extends AFileConfiguration {

	private Header			header;

	private List<Legend>	legends	= new ArrayList<>();

	public ScanConfiguration() {
		super(DirectoryLocator.findPath(Directory.CONFIG).toString(), "scan");
	}

	public Header getHeader() {
		return header;
	}

	public void setHeader(Header header) {
		this.header = header;
	}

	public List<Legend> getLegends() {
		return legends;
	}

	public void setLegends(List<Legend> legends) {
		this.legends = legends;
	}

}
