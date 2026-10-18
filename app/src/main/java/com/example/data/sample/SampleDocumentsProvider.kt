package com.example.data.sample

import com.example.data.model.FormFieldType
import com.example.data.model.PdfBookmark
import com.example.data.model.PdfDocument
import com.example.data.model.PdfFormField
import com.example.data.model.PdfPage

object SampleDocumentsProvider {

    fun getSampleDocuments(): List<PdfDocument> {
        val annualReport = PdfDocument(
            id = "doc_annual_report_2026",
            name = "Q3_Financial_Performance_Report.pdf",
            sizeBytes = 2_458_120L,
            pageCount = 6,
            author = "Morgan Stanley Research",
            title = "Global Market & Fintech Quarterly Insights",
            subject = "Financial Analysis",
            keywords = listOf("Financials", "Fintech", "Q3", "Audit", "Strategy"),
            bookmarks = listOf(
                PdfBookmark("bm_1", "Executive Summary", 0, 0),
                PdfBookmark("bm_2", "Key Performance Indicators", 1, 0),
                PdfBookmark("bm_3", "Revenue Breakdown by Geography", 2, 0),
                PdfBookmark("bm_4", "Operating Expenses & Margin", 3, 0),
                PdfBookmark("bm_5", "Risk Assessment & Hedging", 4, 0),
                PdfBookmark("bm_6", "Forward-Looking Projections", 5, 0)
            ),
            pages = listOf(
                PdfPage(
                    pageIndex = 0,
                    title = "Page 1: Executive Summary",
                    textSnippet = "The third quarter demonstrated extraordinary resilience with 24.8% YoY net revenue growth across enterprise accounts..."
                ),
                PdfPage(
                    pageIndex = 1,
                    title = "Page 2: Key Performance Indicators",
                    textSnippet = "Operating margins expanded 320 basis points to 38.4%, propelled by AI workflow automation and cloud efficiency."
                ),
                PdfPage(
                    pageIndex = 2,
                    title = "Page 3: Regional Distribution",
                    textSnippet = "North America comprised 54% of annualized recurring revenue, followed by EMEA at 29% and APAC at 17%."
                ),
                PdfPage(
                    pageIndex = 3,
                    title = "Page 4: Expense Analysis",
                    textSnippet = "R&D expenditures normalized at 18.2% of sales, funding core architectural migrations and on-device machine intelligence."
                ),
                PdfPage(
                    pageIndex = 4,
                    title = "Page 5: Risk & Governance",
                    textSnippet = "Capital reserves remain fortified with $4.2B in liquid assets, zero outstanding convertible debentures, and AA rating."
                ),
                PdfPage(
                    pageIndex = 5,
                    title = "Page 6: Strategic Roadmap",
                    textSnippet = "Guidance for FY2027 reflects anticipated secular tailwinds, with anticipated full-year gross profit upside of 28%."
                )
            ),
            formFields = listOf(
                PdfFormField("f_auditor_name", "Auditor Name", FormFieldType.TEXT, 0, "Elena Rostova", true),
                PdfFormField("f_verified", "Verified by Compliance", FormFieldType.CHECKBOX, 0, "true"),
                PdfFormField("f_approval_sig", "Officer Signature", FormFieldType.SIGNATURE, 5, "Elena Rostova, CFA", true)
            )
        )

        val legalContract = PdfDocument(
            id = "doc_enterprise_nda_master",
            name = "Mutual_Non_Disclosure_Agreement.pdf",
            sizeBytes = 1_120_840L,
            pageCount = 4,
            author = "Wilson Sonsini Goodrich & Rosati",
            title = "Bilateral Proprietary Information Agreement",
            subject = "Legal & Compliance",
            keywords = listOf("NDA", "Legal", "Proprietary", "Confidential"),
            isEncrypted = false,
            bookmarks = listOf(
                PdfBookmark("bm_nda_1", "1. Definition of Confidential Information", 0, 0),
                PdfBookmark("bm_nda_2", "2. Obligations of Non-Disclosure", 1, 0),
                PdfBookmark("bm_nda_3", "3. Permitted Disclosures & Exclusions", 2, 0),
                PdfBookmark("bm_nda_4", "4. Governing Law & Signatures", 3, 0)
            ),
            pages = (0..3).map { index ->
                PdfPage(
                    pageIndex = index,
                    title = "Clause Section ${index + 1}",
                    textSnippet = when (index) {
                        0 -> "This Bilateral Agreement is entered into by and between the Receiving Party and the Disclosing Party to protect proprietary trade secrets."
                        1 -> "Receiving Party shall hold in strict confidence and take all reasonable precautions to prevent unauthorized dissemination of trade secrets."
                        2 -> "The obligations hereunder shall not apply to information generally accessible to the public without breach of this covenant."
                        else -> "In witness whereof, the parties hereto have executed this Bilateral Covenant as of the Effective Date written below."
                    }
                )
            },
            formFields = listOf(
                PdfFormField("f_company_name", "Receiving Company", FormFieldType.TEXT, 0, "Acme Cloud Systems Inc.", true),
                PdfFormField("f_effective_date", "Effective Date", FormFieldType.TEXT, 0, "October 24, 2026", true),
                PdfFormField("f_signatory_title", "Signatory Title", FormFieldType.TEXT, 3, "Chief Technology Officer"),
                PdfFormField("f_counterparty_sig", "Counterparty Signature", FormFieldType.SIGNATURE, 3, "Alexander Wright", true)
            )
        )

        val engineeringDesign = PdfDocument(
            id = "doc_system_arch_spec",
            name = "Autonomous_Vehicle_Sensor_Fusion_Arch.pdf",
            sizeBytes = 4_890_200L,
            pageCount = 5,
            author = "Applied Robotics Systems Lab",
            title = "High-Bandwidth LiDAR & Radar Telemetry Pipeline",
            subject = "Hardware Architecture",
            keywords = listOf("Robotics", "LiDAR", "Perception", "KalmanFilter"),
            bookmarks = listOf(
                PdfBookmark("bm_eng_1", "Sensory Hardware Topography", 0, 0),
                PdfBookmark("bm_eng_2", "Low-Latency CAN-FD Bus", 1, 0),
                PdfBookmark("bm_eng_3", "Extended Kalman Filter Node", 2, 0),
                PdfBookmark("bm_eng_4", "Obstacle Classification", 3, 0),
                PdfBookmark("bm_eng_5", "Thermal & Fault Recovery", 4, 0)
            ),
            pages = (0..4).map { idx ->
                PdfPage(
                    pageIndex = idx,
                    title = "Architecture Specification Diagram ${idx + 1}",
                    textSnippet = "Subsystem node $idx captures 120Hz point-cloud streams with sub-3ms determinism over automotive ethernet."
                )
            }
        )

        val scannedInvoice = PdfDocument(
            id = "doc_scanned_freight_invoice",
            name = "Scanned_Freight_Invoice_Archive.pdf",
            sizeBytes = 3_740_500L,
            pageCount = 3,
            author = "Apex Logistics Document Scanner",
            title = "Freight & Hardware Optical Scan Archive",
            subject = "Scanned Invoices & Customs Declarations",
            keywords = listOf("Scanned", "Invoice", "OCR", "Apex", "Customs"),
            isScannedDocument = true,
            bookmarks = listOf(
                PdfBookmark("bm_scan_1", "Page 1: Freight Bill of Lading", 0, 0),
                PdfBookmark("bm_scan_2", "Page 2: Hardware Delivery Receipt", 1, 0),
                PdfBookmark("bm_scan_3", "Page 3: Customs & Tax Stamp", 2, 0)
            ),
            pages = listOf(
                PdfPage(
                    pageIndex = 0,
                    title = "Page 1: Bill of Lading (Scanned)",
                    textSnippet = "[Scanned Optical Raster Image - No embedded PDF font layer]",
                    isScannedImage = true,
                    ocrText = null,
                    isOcrProcessed = false
                ),
                PdfPage(
                    pageIndex = 1,
                    title = "Page 2: Equipment Manifest (Scanned)",
                    textSnippet = "[Scanned Optical Raster Image - No embedded PDF font layer]",
                    isScannedImage = true,
                    ocrText = null,
                    isOcrProcessed = false
                ),
                PdfPage(
                    pageIndex = 2,
                    title = "Page 3: Customs Certificate (Scanned)",
                    textSnippet = "[Scanned Optical Raster Image - No embedded PDF font layer]",
                    isScannedImage = true,
                    ocrText = null,
                    isOcrProcessed = false
                )
            )
        )

        return listOf(scannedInvoice, annualReport, legalContract, engineeringDesign)
    }
}
