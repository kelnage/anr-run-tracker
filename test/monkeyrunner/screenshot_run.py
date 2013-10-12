# Copyright (C) 2013 Nick Moore
#
# This file is part of ANR Run Tracker
#
# ANR Run Tracker is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.

import sys, os, time
from com.android.monkeyrunner import MonkeyRunner, MonkeyDevice

delay = 5
output_directory = os.path.dirname(sys.argv[0])

device = MonkeyRunner.waitForConnection()

package = 'uk.org.nickmoore.runtrack'

activities = {'main_activity': 'uk.org.nickmoore.runtrack.ui.MainActivity',
    'player_stats': 'uk.org.nickmoore.runtrack.ui.PlayerStatsActivity'}

for (filename, activity) in activities.iteritems():
    runComponent = package + '/' + activity
    device.startActivity(component=runComponent)
    time.sleep(delay)
    image = device.takeSnapshot()
    image.writeToFile(os.path.join(output_directory, filename + '.png'),'png')

runComponent = package + '/uk.org.nickmoore.runtrack.ui.GameActivity'
device.startActivity(component=runComponent)
time.sleep(delay)
opponents = device.takeSnapshot()
opponents.writeToFile(os.path.join(output_directory, 'opponents.png'),'png')

device.press('KEYCODE_DPAD_UP', 'DOWN_AND_UP')
device.press('KEYCODE_DPAD_CENTER', 'DOWN_AND_UP')
time.sleep(delay)

new_game = device.takeSnapshot()
new_game.writeToFile(os.path.join(output_directory, 'new_game.png'),'png')