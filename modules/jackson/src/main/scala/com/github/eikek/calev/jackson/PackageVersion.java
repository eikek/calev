package com.github.eikek.calev.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.Versioned;
import com.fasterxml.jackson.core.util.VersionUtil;

public final class PackageVersion implements Versioned {
	public final static Version VERSION = VersionUtil.parseVersion(
			"1.0.0", "com.github.eikek.calev", "calev-jackson"
	);

	@Override
	public Version version() {
		return VERSION;
	}
}
