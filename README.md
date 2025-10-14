# Metal Investment
<div>
<div dir="auto">
<p>This project is for Revolut users from Romania who invested in gold, silver or platinum.<br />The project is monitoring the precious metals price at Bloomberg/Galmarley, and it's using some formula to approximate the metal price at Revolut. If the price of the metal is so high so your profit would match a logical expression that you provided, then the application would notify you by email. Also the users could check whenever they want their profit by calling an API. The project is in developing phase. The project is monitoring the precious metals price at Bloomberg and Galmarley, and it's using some formula to approximate the metal price at Revolut.</p>
</div>

<section class="mb-12 material-card">
  <h2 class="text-2xl font-bold text-[#073B4C] mb-4">Technical Stack</h2>
  <ul class="list-disc list-inside text-gray-700 space-y-2">
    <li><span class="font-bold">Java 25:</span> Core programming language for the application logic.</li>
    <li><span class="font-bold">Spring Boot 3.5.6:</span> Framework for backend of a webapp with typical starters: web, data‑JPA, security, mail, actuator.</li>
    <li><span class="font-bold">React 18.2.0:</span> Frontend library for building user interfaces with component-based architecture.</li>
    <li><span class="font-bold">Swagger 2.8.13:</span> API documentation and testing interface using SpringDoc OpenAPI for interactive API exploration.</li>
    <li><span class="font-bold">Lombok 1.18.40:</span> Java library that automatically generates boilerplate code like getters, setters, constructors, and equals methods.</li>
    <li><span class="font-bold">Google Cloud Platform:</span> Cloud deployment platform with Cloud Run, Cloud SQL, and scalable infrastructure.</li>
    <li><span class="font-bold">MySQL 8.0:</span> Database.</li>
    <li><span class="font-bold">Liquibase 4.31.1:</span> Library for tracking, managing and applying database schema changes.</li>
    <li><span class="font-bold">Resilience4j 2.2.0:</span> Lightweight fault tolerance library that implements resilience patterns, like circuit breakers, rate limiters, and retries, to make applications more robust and able to handle failures gracefully</li>
    <li><span class="font-bold">Unirest 3.14.5:</span> HTTP client library for making API calls to fetch real-time metal prices.</li>
    <li><span class="font-bold">External APIs:</span> Bloomberg and Galmarley APIs for fetching real-time precious metal prices with market data integration.</li>
    <li><span class="font-bold">Hibernate 6.6.25:</span> Caching integration for database.</li>
    <li><span class="font-bold">Maven 3.14.0:</span> Build automation tool for project management and dependency handling.</li>
    <li><span class="font-bold">Git:</span> Version control system for collaborative development and code management.</li>
  </ul>

  The application was deployed to <b>Google Cloud (GCP)</b> byt SQL service is stopped for cost reasons
  <br>https://metal-investment-635786220311.europe-west1.run.app/actuator/health </br>

</section>

<section class="mb-12 material-card">
  <h2 class="text-2xl font-bold text-[#073B4C] mb-4">Documentation</h2>
  <ul class="list-disc list-inside text-gray-700 space-y-2">
    <li><a href="API_DOCUMENTATION.md" class="text-blue-600 hover:text-blue-800 underline">API Documentation</a> - Complete API reference with Swagger annotations and endpoint details</li>
    <li><a href="README-UI.md" class="text-blue-600 hover:text-blue-800 underline">UI Documentation</a> - React frontend documentation with build process and deployment guide</li>
  </ul>
</section>