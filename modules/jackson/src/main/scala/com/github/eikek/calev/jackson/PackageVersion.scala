package com.github.eikek.calev.jackson

import com.fasterxml.jackson.core.util.VersionUtil
import com.fasterxml.jackson.core.{Version, Versioned}

object PackageVersion {
  val VERSION: Version =
    VersionUtil.parseVersion("1.0.0", "com.github.eikek.calev", "calev-jackson")
}
final class PackageVersion extends Versioned {
  override def version: Version = PackageVersion.VERSION
}
