/*
 * LanDiscovery - Broadcast the server this plugin is running on, as if it were a LAN server
 * Copyright ©2015-2020 zml
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.stellardrift.landiscovery;

class ProjectData {
    public static final String ARTIFACT_ID = "{{ name | lower }}";
    public static final String NAME = "{{ name }}";
    public static final String VERSION = "{{ version }}";
}
