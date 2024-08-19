# GiftAndGo
- Returning a concatenated string of errors for now. A better option would have been to return a list.
- The requirement was to only log the request coming to the file processing endpoint to the database. Since this is the only endpoint in the application for now, I am not checking whether the request is for the file processing endpoint.
- Name of the blocked ISPs in the exercise is 
  AWS,
  GCP and 
  Azure
But when I looked at the documentation of the external location API, the ISP name is
Google and not GCP, so my assumption is it would be Microsoft for Azure and Amazon for AWS.
- I have used CountryCode to check for the country instead of countryName. 
- Even when validation is disabled via the feature flag, since I need to parse the average and top speed into numbers, there will be validation when I call Double.parseDouble().
- I haven't added the validation for the format of the ID field in the file. We can optionally add a regex validation for this field.