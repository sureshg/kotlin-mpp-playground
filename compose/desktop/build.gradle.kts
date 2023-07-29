import common.mppTargetName

plugins { plugins.kotlin.mpp }

mppTargetName = "desktop"

dependencies { commonMainImplementation(projects.common) }
