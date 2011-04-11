
/*
 * Processing Audio Visualization (PAV)
 * Copyright (C) 2011  Christopher Pramerdorfer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

uniform sampler2D Texture;
uniform vec4 Color;

void main(void)
{
    vec2 st = gl_TexCoord[0].st;
	vec4 tex = texture2D(Texture, st);
    
    if(tex.a > 0.0 && (tex.r > 0.0 || tex.g > 0.0 || tex.b > 0.0)) {
    	gl_FragColor = Color;
    }
    else {
    	gl_FragColor = vec4(0.0);
    }
}

