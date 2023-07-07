import common.mppTargetName

plugins { plugins.kotlin.mpp }

mppTargetName = "desktop"

dependencies { implementation(projects.common) }
