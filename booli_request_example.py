import argparse
import getopt
import json
import random
import string
import sys
import time
from hashlib import sha1

import requests

"""
Make a sample call to the Booli API asking for all listings in 'Nacka' in JSON format, 
using 'YOUR_CALLER_ID' and 'YOUR_PRIVATE_KEY' for authentication
"""




def main(argv):
    def get_booli_data(callerId, timestamp, unique, hashstr, limit, offset, query, type):
        headers = {'Accept': 'application/vnd.booli-v2+json'}
        url = "http://api.booli.se/" + type + "?q=" + query + "&limit=" + str(limit) + "&offset=" + str(
            offset) + "&callerId=" + callerId + "&time=" + timestamp + "&unique=" + unique + "&hash=" + hashstr
        r = requests.get(url, headers=headers)
        r.encoding = 'utf-8'

        if (r.status_code != 200):
            print("fail")
        global result
        result = json.loads(r.text)
        return result

    def send_event_to_splunk(splunk_host, splunk_auth_token, listing):
        try:
            # Integer value representing epoch time format
            listTime = listing.get("published")
            pattern = '%Y-%m-%d %H:%M:%S'
            event_time = int(time.mktime(time.strptime(listTime, pattern)))

            # String representing the host name or IP
            host_id = "localhost"

            # String representing the Splunk sourcetype, see:
            # docs.splunk.com/Documentation/Splunk/6.3.2/Data/Listofpretrainedsourcetypes
            source_type = "_json"

            # Create request URL
            request_url = "https://%s:8088/services/collector" % splunk_host

            post_data = {
                "time": event_time,
                "host": host_id,
                "sourcetype": source_type,
                "event": listing
            }

            # Encode data in JSON utf-8 format
            data = json.dumps(post_data).encode('utf8')

            # Create auth header
            auth_header = "Splunk %s" % splunk_auth_token
            headers = {'Authorization': auth_header}

            # Create request
            response = requests.post(request_url, headers=headers, data=data, verify=False)
            response.raise_for_status()

            # read response, should be in JSON format
            read_response = response.text

            try:
                response_json = json.loads(read_response)

                if "text" in response_json:
                    if response_json["text"] == "Success":
                        post_success = True
                    else:
                        post_success = False
            except:
                post_success = False

            if post_success == True:
                # Event was recieved successfully
                print ("Event was recieved successfully")
            else:
                # Event returned an error
                print ("Error sending request.")

        except Exception as err:
            # Network or connection error
            post_success = False
            print ("Error sending request")
            print (str(err))

        return post_success
    callerId = "<YOUR_CALLER_ID>"
    privateKey = "<YOUR_PRIVATE_KEY>"
    timestamp = str(int(time.time()))
    unique = ''.join(random.choice(string.ascii_uppercase + string.digits) for x in range(16))
    hashstr = sha1((callerId + timestamp + privateKey + unique).encode('utf-8')).hexdigest()
    splunk_host = "localhost"
    splunk_auth_token = "b72865fa-31ec-4ce0-a7fb-518bdef55c2e"
    limit = 500
    offset = 0
    totalCount = 1
    success = True
    while totalCount > offset or success:
        result = get_booli_data(callerId, timestamp, unique, hashstr, limit, offset, args.query, args.type)
        offset = result.get("offset")
        offset += result.get("count")
        listings = result.get("sold")
        for listing in listings:
            success = send_event_to_splunk(splunk_host, splunk_auth_token, listing)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Set Booli Query and Type of Object (sold or listing')
    parser.add_argument('-q', '--query', help='City, Area to query Booli API, default = stockholm', default="stockholm")
    parser.add_argument('-t', '--type', help='Type of the Booli data to retrive, Sold or Listed Objects', default="sold")
    args = parser.parse_args()
    main(args)









