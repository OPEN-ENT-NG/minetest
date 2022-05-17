# minetest

The Minetest application (open-source version of Minecraft) allows the creation of worlds in which 
students user type can collaborate to achieve different goals. The worlds are randomly generated with blocks,
players must collect various raw materials in order to shape their world as they wish

# About
* Licence : [AGPL v3](http://www.gnu.org/licenses/agpl.txt) - Copyright Région Nouvelle Aquitaine
* Developer : CGI
* Financer : Copyright Région Nouvelle Aquitaine
* Description : The Minetest application (open-source version of Minecraft) allows the creation of worlds in which
  students user type can collaborate to achieve different goals.
* Features :
   - The teacher can create different Minetest world on ENT interface application.
   - The teacher can invite its students by sharing with them the link to the Minetest world. 
   - The student can only access to a world in which he was invited to.
   - The student can manage (add / delete) its list of users who have access to its Minetest world.

## Configuration
Specific configuration that must be seen :
<pre>
{
  "config": {
    ...
    "minetest-download": "${minetestDownload}",
    "minetest-server": "${minetestServer}",
    "minetest-python-server-port": "${minetestPythonServerPort}",
    "minetest-port-range": "${minetestPortRange}"
  }
}
</pre>

In your springboard, you must include these variables :
<pre>
minetestDownload = ${String}
minetestServer = ${String}
minetestPythonServerPort = Integer
minetestPortRange = ${String}
</pre>