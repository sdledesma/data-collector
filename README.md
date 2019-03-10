# data-collector

A small library to create a collector that receives data on the background

## Usage

You can create a collector calling (create-collector) and passing it a max time between updates (in milliseconds), the function used to query the data and the arguments needed by the function (if any).

If for any reason the query function throws an exception, the value returned by get-data will not be updated.

The returned collector can be used to see the data returned and to stop and force refresh the collector

(get-data [collector]) will return the latest queried data
(refresh-collector [collector]) will force a call to the query function
(shutdown-collector [collector]) will stop the collector for good. a stopped collector will return nil when get-data is called.

## License

Copyright © 2019 Santiago de Ledesma

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
