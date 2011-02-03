
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

uniform float Add;
uniform sampler2D Age;
uniform sampler2D New;

void main(void)
{
    vec2 st = gl_TexCoord[0].st;
    float age = texture2D(Age, st).r;
    float new = texture2D(New, st).a;

	if(new != 0.0) {
		gl_FragColor = vec4(0.0);
	}
	else {
		gl_FragColor = vec4(age + Add);
	}
}