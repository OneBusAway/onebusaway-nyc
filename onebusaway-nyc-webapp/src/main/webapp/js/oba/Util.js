// Copyright 2010, OpenPlans
// Licensed under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

var OBA = window.OBA || {};

OBA.Util = (function() {
    return {
    	truncate: function(text, length) {
            // FIXME: truncate on word boundaries?
            if(typeof text === 'string' && text.length > length) {
                text = text.substr(0, length - 3) + "...";
            }

            return text;
        },
        serializeArray: function(lst, keyname) {
        	var result = null;

            jQuery.each(lst, function(i, x) {
            	if (!result) {
            		result = keyname + "=" + x;
            	} else {
            		result += "&" + keyname + "=" + x;
            	}
            });

            return result;
        }
    };
})();
